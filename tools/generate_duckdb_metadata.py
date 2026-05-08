#!/usr/bin/env python3

import csv
import os
import re
import subprocess
from pathlib import Path
from xml.sax.saxutils import escape

ROOT = Path(
    os.environ.get(
        "DUCKDB_METADATA_ROOT",
        str(Path(__file__).resolve().parent.parent),
    )
)
DUCKDB_BIN = os.environ.get("DUCKDB_BIN", "duckdb")
DIALECT_JAVA = ROOT / "src/main/java/com/amfaro/duckdb/dialect"
DIALECT_RES = ROOT / "src/main/resources/com/amfaro/duckdb/dialect"
FUNCTION_NAME_RE = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def query_csv(sql: str) -> list[dict[str, str]]:
    out = subprocess.check_output(
        [DUCKDB_BIN, "-csv", "-c", sql],
        text=True,
        stderr=subprocess.STDOUT,
    )
    return list(csv.DictReader(out.splitlines()))


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def java_keywords_interface(name: str, keywords: list[str]) -> str:
    body = "\n".join(
        f'    SqlKeywordTokenType {keyword} = new SqlKeywordTokenType("{keyword}");'
        for keyword in keywords
    )
    return (
        "package com.amfaro.duckdb.dialect;\n\n"
        "import com.intellij.sql.psi.SqlKeywordTokenType;\n\n"
        f"public interface {name} {{\n{body}\n}}\n"
    )


def prototype_for(arity: int) -> str:
    if arity <= 0:
        return "():ANY"
    if arity == 1:
        return "(arg1:ANY):ANY"
    args = ", ".join(f"arg{i}:ANY" for i in range(1, arity + 1))
    return f"({args}):ANY"


def parse_arity(value: str) -> int:
    value = value.strip()
    if not value or value == "NULL":
        return 0
    if value.isdigit():
        return int(value)
    if value.startswith("[") and value.endswith("]"):
        inner = value[1:-1].strip()
        if not inner:
            return 0
        return len([part for part in inner.split(",") if part.strip()])
    raise ValueError(f"unsupported arity value: {value}")


def normalize_functions(
    rows: list[dict[str, str]], *, name_key: str, type_key: str, arity_key: str
) -> list[dict[str, str]]:
    aggregate_by_name: dict[str, bool] = {}
    min_arity_by_name: dict[str, int] = {}

    for row in rows:
        function_name = row[name_key]
        if not FUNCTION_NAME_RE.match(function_name):
            continue

        is_aggregate = row[type_key].lower() == "aggregate"
        arity = parse_arity(row[arity_key] or "0")

        aggregate_by_name[function_name] = (
            aggregate_by_name.get(function_name, False) or is_aggregate
        )
        min_arity_by_name[function_name] = min(
            min_arity_by_name.get(function_name, arity), arity
        )

    return [
        {
            "function_name": function_name,
            "is_aggregate": "1" if aggregate_by_name[function_name] else "0",
            "min_arity": str(min_arity_by_name[function_name]),
        }
        for function_name in sorted(min_arity_by_name)
    ]


def load_functions() -> list[dict[str, str]]:
    try:
        return normalize_functions(
            query_csv(
                "select function_name, function_type, "
                "coalesce(array_length(parameters), 0) as arity "
                "from duckdb_functions()"
            ),
            name_key="function_name",
            type_key="function_type",
            arity_key="arity",
        )
    except subprocess.CalledProcessError:
        return normalize_functions(
            query_csv("PRAGMA functions"),
            name_key="name",
            type_key="type",
            arity_key="parameters",
        )


def main() -> None:
    keywords = query_csv(
        "select upper(keyword_name) as keyword_name, keyword_category "
        "from duckdb_keywords() order by keyword_name"
    )
    reserved = [
        row["keyword_name"] for row in keywords if row["keyword_category"] == "reserved"
    ]
    optional = [
        row["keyword_name"] for row in keywords if row["keyword_category"] != "reserved"
    ]

    functions = load_functions()

    write(
        DIALECT_JAVA / "DuckDbReservedKeywords.java",
        java_keywords_interface("DuckDbReservedKeywords", reserved),
    )
    write(
        DIALECT_JAVA / "DuckDbOptionalKeywords.java",
        java_keywords_interface("DuckDbOptionalKeywords", optional),
    )
    write(DIALECT_RES / "duckdb-keywords-reserved.txt", "\n".join(reserved) + "\n")
    write(DIALECT_RES / "duckdb-keywords-optional.txt", "\n".join(optional) + "\n")
    write(
        DIALECT_RES / "duckdb-functions.txt",
        "\n".join(row["function_name"] for row in functions) + "\n",
    )

    lines = ['<?xml version="1.0" encoding="UTF-8"?>', "<functions>"]
    for row in functions:
        name = escape(row["function_name"])
        aggregate = " aggregate='true'" if row["is_aggregate"] == "1" else ""
        lines.append(f"  <function{aggregate}>")
        lines.append(f"    <name>{name}</name>")
        lines.append(
            f"    <prototype>{prototype_for(int(row['min_arity']))}</prototype>"
        )
        lines.append("  </function>")
    lines.append("</functions>")
    write(DIALECT_RES / "functions.xml", "\n".join(lines) + "\n")


if __name__ == "__main__":
    main()

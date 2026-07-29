#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

RENAMES = {
    "FakeAuthRepository": "AuthRepository",
    "FakeBookingRepository": "BookingRepository",
    "FakeClientProfileRepository": "ClientProfileRepository",
    "FakeMastersRepository": "MastersRepository",
    "FakeReviewsRepository": "ReviewsRepository",
}

IMPLEMENTATION_FILES = {
    "shared/src/commonMain/kotlin/com/beautyplanner/client/fake/FakeAuthRepository.kt": (
        "AuthRepository",
        "com.beautyplanner.client.domain.repository.AuthRepository",
    ),
    "shared/src/commonMain/kotlin/com/beautyplanner/client/fake/FakeBookingRepository.kt": (
        "BookingRepository",
        "com.beautyplanner.client.domain.repository.BookingRepository",
    ),
    "shared/src/commonMain/kotlin/com/beautyplanner/client/fake/FakeClientProfileRepository.kt": (
        "ClientProfileRepository",
        "com.beautyplanner.client.domain.repository.ClientProfileRepository",
    ),
    "shared/src/commonMain/kotlin/com/beautyplanner/client/fake/FakeMastersRepository.kt": (
        "MastersRepository",
        "com.beautyplanner.client.domain.repository.MastersRepository",
    ),
    "shared/src/commonMain/kotlin/com/beautyplanner/client/fake/FakeReviewsRepository.kt": (
        "ReviewsRepository",
        "com.beautyplanner.client.domain.repository.ReviewsRepository",
    ),
}

TEXT_FILE_SUFFIXES = {".kt", ".kts", ".md", ".txt"}

SKIP_DIRS = {
    ".git",
    ".gradle",
    ".idea",
    "build",
    ".kotlin",
    "__pycache__",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Rename Fake*Repository classes/files/usages to clean names."
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Actually write changes. Without this flag, runs in dry-run mode.",
    )
    return parser.parse_args()


def project_root() -> Path:
    return Path.cwd()


def should_skip(path: Path) -> bool:
    return any(part in SKIP_DIRS for part in path.parts)


def iter_text_files(root: Path):
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if should_skip(path):
            continue
        if path.suffix.lower() in TEXT_FILE_SUFFIXES:
            yield path


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write_text(path: Path, content: str) -> None:
    path.write_text(content, encoding="utf-8")


def replace_all_names(text: str) -> str:
    for old, new in RENAMES.items():
        text = text.replace(old, new)
    return text


def fix_kotlin_class_conflict(text: str, class_name: str, fq_interface: str) -> str:
    import_pattern = re.compile(
        rf"^import\s+{re.escape(fq_interface)}\s*$",
        re.MULTILINE,
    )
    text = import_pattern.sub("", text)

    class_pattern = re.compile(
        rf"\bclass\s+{re.escape(class_name)}\s*:\s*{re.escape(class_name)}\b"
    )
    text = class_pattern.sub(
        f"class {class_name} : {fq_interface}",
        text,
    )

    text = re.sub(r"\n{3,}", "\n\n", text)
    return text


def renamed_relative_path(rel_path: str) -> str:
    updated = rel_path
    for old, new in RENAMES.items():
        updated = updated.replace(old, new)
    return updated


def rename_file_if_needed(path: Path, apply: bool) -> tuple[Path, bool]:
    new_name = path.name
    for old, new in RENAMES.items():
        if old in new_name:
            new_name = new_name.replace(old, new)

    if new_name == path.name:
        return path, False

    new_path = path.with_name(new_name)
    print(f"[rename-file] {path} -> {new_path}")
    if apply:
        path.rename(new_path)
        return new_path, True

    return path, True


def process_text_file(path: Path, root: Path, apply: bool) -> bool:
    original = read_text(path)
    updated = replace_all_names(original)

    rel_before = path.relative_to(root).as_posix()

    if rel_before in IMPLEMENTATION_FILES:
        class_name, fq_interface = IMPLEMENTATION_FILES[rel_before]
        updated = fix_kotlin_class_conflict(updated, class_name, fq_interface)

    if updated != original:
        print(f"[update-text] {path}")
        if apply:
            write_text(path, updated)
        return True
    return False


def main() -> int:
    args = parse_args()
    root = project_root()

    print(f"Project root: {root}")
    print("Mode:", "APPLY" if args.apply else "DRY-RUN")
    print()

    files = list(iter_text_files(root))
    changed = 0

    for path in files:
        if process_text_file(path, root, args.apply):
            changed += 1

    print()
    print("Pass 1 complete: text replacements done.")
    print()

    renamed = 0
    post_rename_targets: list[tuple[Path, str, str]] = []

    candidate_files = sorted(
        [p for p in root.rglob("*") if p.is_file() and not should_skip(p)],
        key=lambda p: len(p.as_posix()),
        reverse=True,
    )

    for path in candidate_files:
        original_rel = path.relative_to(root).as_posix()
        maybe_new_path, was_renamed = rename_file_if_needed(path, args.apply)
        if was_renamed:
            renamed += 1

        if original_rel in IMPLEMENTATION_FILES:
            class_name, fq_interface = IMPLEMENTATION_FILES[original_rel]
            target_rel = renamed_relative_path(original_rel)
            target_path = maybe_new_path if args.apply else (root / target_rel)
            post_rename_targets.append((target_path, class_name, fq_interface))

    if args.apply:
        for path, class_name, fq_interface in post_rename_targets:
            if not path.exists():
                continue
            original = read_text(path)
            updated = fix_kotlin_class_conflict(original, class_name, fq_interface)
            if updated != original:
                print(f"[post-rename-fix] {path}")
                write_text(path, updated)
    else:
        for path, class_name, fq_interface in post_rename_targets:
            print(f"[post-rename-fix] {path}")

    print()
    print(f"Text files updated: {changed}")
    print(f"Files renamed: {renamed}")
    print()

    if not args.apply:
        print("Dry run only. Re-run with --apply to write changes.")
    else:
        print("Done.")
        print("Recommended next steps:")
        print("1) Search project for: Fake")
        print("2) Build project")
        print("3) Review git diff")

    return 0


if __name__ == "__main__":
    sys.exit(main())

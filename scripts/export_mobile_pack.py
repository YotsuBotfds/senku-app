#!/usr/bin/env python3
"""Export the current Senku corpus and embeddings into an offline mobile pack."""

import argparse
import json
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[1]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

import config  # noqa: E402
from mobile_pack import (  # noqa: E402
    DEFAULT_BATCH_SIZE,
    DEFAULT_COLLECTION_NAME,
    DEFAULT_MOBILE_TOP_K,
    export_mobile_pack_from_chroma,
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "output_dir",
        help="Directory that will receive senku_mobile.sqlite3, senku_vectors.*, and senku_manifest.json",
    )
    parser.add_argument(
        "--collection-name",
        default=DEFAULT_COLLECTION_NAME,
        help=f"Chroma collection name (default: {DEFAULT_COLLECTION_NAME})",
    )
    parser.add_argument(
        "--chroma-dir",
        default=config.CHROMA_DB_DIR,
        help=f"Chroma database directory (default: {config.CHROMA_DB_DIR})",
    )
    parser.add_argument(
        "--guides-dir",
        default=config.COMPENDIUM_DIR,
        help=f"Guides directory (default: {config.COMPENDIUM_DIR})",
    )
    parser.add_argument(
        "--embedding-model-id",
        default=config.EMBED_MODEL,
        help=f"Manifest embedding model id (default: {config.EMBED_MODEL})",
    )
    parser.add_argument(
        "--vector-dtype",
        choices=("float16", "int8"),
        default="float16",
        help="Vector payload format for exported chunk embeddings (default: float16)",
    )
    parser.add_argument(
        "--mobile-top-k",
        type=int,
        default=DEFAULT_MOBILE_TOP_K,
        help=f"Recommended mobile retrieval top-k to record in the manifest (default: {DEFAULT_MOBILE_TOP_K})",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=DEFAULT_BATCH_SIZE,
        help=f"Chunk fetch batch size when reading from Chroma (default: {DEFAULT_BATCH_SIZE})",
    )
    args = parser.parse_args()

    summary = export_mobile_pack_from_chroma(
        args.output_dir,
        chroma_dir=args.chroma_dir,
        guides_dir=args.guides_dir,
        collection_name=args.collection_name,
        embedding_model_id=args.embedding_model_id,
        vector_dtype=args.vector_dtype,
        mobile_top_k=args.mobile_top_k,
        batch_size=args.batch_size,
    )
    print(json.dumps(summary.__dict__, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()

"""Bench configuration parsing helpers."""

from collections.abc import Iterable, Mapping

from lmstudio_utils import normalize_lm_studio_url


def _split_csv(value):
    return [item.strip() for item in value.split(",") if item.strip()]


def parse_url_list(value, default_url=None, *, dedupe=False):
    """Parse configured bench URLs without treating malformed scalars as iterables."""
    if isinstance(value, str):
        urls = _split_csv(value)
        if not urls and default_url:
            urls = [default_url]
    elif value is None or value is False:
        urls = [default_url] if default_url else []
    elif isinstance(value, Mapping) or not isinstance(value, Iterable):
        urls = [default_url] if default_url else []
    else:
        urls = [str(item).strip() for item in value if str(item).strip()]

    if not dedupe:
        return urls

    seen = set()
    unique_urls = []
    for url in urls:
        normalized = normalize_lm_studio_url(url) or url
        if normalized in seen:
            continue
        seen.add(normalized)
        unique_urls.append(url)
    return unique_urls

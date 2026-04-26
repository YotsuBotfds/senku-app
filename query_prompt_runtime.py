"""Shared prompt-budget helpers for query and bench runtimes."""


def system_prompt_text(config_module, mode):
    """Return the system prompt text used for chat generation."""
    return (
        config_module.build_system_prompt(mode)
        if hasattr(config_module, "build_system_prompt")
        else config_module.SYSTEM_PROMPT
    )


def prompt_token_limit_from_config(config_module, gen_model=None, gen_url=None):
    """Return the configured prompt-window limit via the runtime config module."""
    return config_module.get_runtime_prompt_token_limit(gen_model, gen_url)


def prompt_token_limit_from_runtime_profile(
    gen_model=None,
    gen_url=None,
    *,
    runtime_profile=None,
    runtime_profile_getter,
):
    """Return the prompt-window limit from a provided or resolved runtime profile."""
    profile = (
        runtime_profile
        if runtime_profile is not None
        else runtime_profile_getter(gen_model, gen_url)
    )
    configured = profile.get("prompt_token_limit")
    if configured is None:
        raise ValueError(
            f"Runtime profile {profile.get('name', '<unknown>')!r} is missing required prompt_token_limit"
        )
    return int(configured)


def safe_prompt_token_limit(prompt_token_limit, prompt_safety_margin=96):
    """Return the prompt assembly limit after reserving generation safety margin."""
    if prompt_token_limit is None:
        return None
    return max(int(prompt_token_limit) - int(prompt_safety_margin), 0)


def estimate_chat_prompt_tokens(
    prompt_text,
    *,
    estimate_tokens_fn,
    system_prompt_resolver,
    use_system_prompt=True,
    mode="default",
    system_prompt_text=None,
):
    """Estimate tokens for a chat request payload before generation."""
    message_overhead = 24
    prompt_tokens = estimate_tokens_fn(prompt_text)
    total = prompt_tokens + message_overhead
    system_prompt_tokens = 0
    if use_system_prompt:
        resolved_system_prompt = (
            system_prompt_resolver(mode)
            if system_prompt_text is None
            else system_prompt_text
        )
        system_prompt_tokens = estimate_tokens_fn(resolved_system_prompt)
        total += system_prompt_tokens + message_overhead
    return {
        "prompt_text_tokens": prompt_tokens,
        "system_prompt_tokens": system_prompt_tokens,
        "estimated_prompt_tokens": total,
    }

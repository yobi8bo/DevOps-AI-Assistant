INSERT INTO ops_model_config (
    provider,
    api_style,
    model_name,
    api_base_url,
    api_key_encrypted,
    max_tokens,
    temperature,
    timeout_seconds,
    default_model,
    status,
    created_by,
    updated_by
)
SELECT
    'OpenAI',
    'RESPONSES',
    'gpt-5.5',
    'https://api.nexustokenai.com',
    NULL,
    4096,
    0.30,
    60,
    1,
    1,
    1,
    1
WHERE NOT EXISTS (
    SELECT 1 FROM ops_model_config WHERE default_model = 1 AND deleted = 0
);

"""Application configuration — replaces CICS system variables and JCL parameters."""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Application
    app_name: str = "CardDemo"
    app_version: str = "1.0.0"
    debug: bool = False

    # Database (replaces VSAM file definitions)
    database_url: str = "postgresql://carddemo:carddemo@localhost:5432/carddemo"

    # JWT Auth (replaces RACF security)
    secret_key: str = "change-me-in-production-use-openssl-rand-hex-32"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 60

    # Celery / Redis (replaces JES2 batch scheduling and MQ)
    redis_url: str = "redis://localhost:6379/0"
    celery_broker_url: str = "redis://localhost:6379/0"
    celery_result_backend: str = "redis://localhost:6379/0"

    # Data files path (replaces DD statements in JCL)
    data_dir: str = "app/data/ASCII"

    model_config = {"env_prefix": "CARDDEMO_", "env_file": ".env", "extra": "ignore"}


settings = Settings()

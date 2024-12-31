package dev.prabhat.models.config

case class LockboxConfig(
  algorithm: AlgorithmConfig,
  fileNameAlgorithms: Option[FileNameAlgorithmConfig],
  settings: SettingsConfig,
  removeAfterEncryption: Boolean
)

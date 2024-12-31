# Lockbox

**Lockbox** is a secure file encryption and decryption tool designed to ensure your sensitive data remains protected. With support for both AES and RSA encryption algorithms, Lockbox allows you to securely encrypt file contents and file names, manage configurations, and handle file operations efficiently.

## Features

- **Encrypt and Decrypt**: Securely encrypt and decrypt file contents with ease.
- **Double Encryption**: Combine AES and RSA for added security.
- **Encrypt and Decrypt File Names**: Obscure sensitive file names through encryption.
- **Configuration Management**: Flexible and dynamic configuration using properties files and command-line arguments.

## Supported Algorithms

Lockbox also supports **Double Encryption**, combining AES and RSA to provide an additional layer of security for your files.

Lockbox currently supports the following encryption algorithms:

- **AES (Advanced Encryption Standard)**: A symmetric key encryption algorithm that is fast and widely used.
- **RSA (Rivest–Shamir–Adleman)**: An asymmetric encryption algorithm that uses a public-private key pair for secure encryption and decryption.

## Getting Started

### Prerequisites

- **Scala**: Version 3.3.4 or later
- **SBT**: Version 1.8.0 or later

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/lockbox.git
   cd lockbox
   ```

2. Install dependencies:
   ```bash
   sbt update
   ```

3. Compile the project:
   ```bash
   sbt compile
   ```

4. Run the application:
   ```bash
   sbt run
   ```

## Usage

### Command-line Arguments

Lockbox supports several command-line arguments for flexible operation:

- **`--help`**: Display this help text.
- **`--version`, `-v`**: Print the version number and exit.
- **`--config <string>`, `-c <string>`**: Path to the configuration file.
- **`--operation <operation>`, `-o <operation>`**: Specify the operation to perform (`encrypt`, `decrypt`).
- **`--encrypt <string>`, `-e <string>`**: Path to the folder to encrypt.
- **`--decrypt <string>`, `-d <string>`**: Path to the folder to decrypt.

#### Example:

Encrypt all files in a directory using the shell script:
```bash
./playground/bin/run-lockbox.sh --config ./playground/config/config.properties --operation encrypt -e ./playground/encrypted/ -d ./playground/decrypted/
```

Decrypt all files in a directory using the shell script:
```bash
./playground/bin/run-lockbox.sh --config ./playground/config/config.properties --operation decrypt -e ./playground/encrypted/ -d ./playground/decrypted/
```

## Configuration

Lockbox uses a `.properties` file for its configuration. Below is an example configuration:

```properties
# Supported encryption algorithms: RSA, AES
lockbox.algorithm.primary=AES

# Optional secondary encryption algorithm
lockbox.algorithm.secondary=RSA

# AES-specific settings
lockbox.settings.AES.key=MySuperSecretKey123!

# RSA-specific settings
lockbox.settings.RSA.publicKeyFile=./keys/public_key.pem
lockbox.settings.RSA.privateKeyFile=./keys/private_key.pem

# File name encryption algorithm
lockbox.algorithm.fileName=AES

# Remove source files after encryption
lockbox.remove-after-encryption=true
```

## Development

### Code Formatting

The project uses `scalafmt` for code formatting. It is integrated into the `compile` task. To manually format the code:
```bash
sbt scalafmtAll
```

### Testing

Run unit tests using:
```bash
sbt test
```

## Contributions

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add your feature"
   ```
4. Push to the branch:
   ```bash
   git push origin feature/your-feature
   ```
5. Create a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
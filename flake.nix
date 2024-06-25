{
  description = "ytm-kt development environment";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    custom_nixpkgs.url = "github:toasterofbread/nixpkgs/adfc9ab8ad2c0824facdb2feea0b03646d584458";
    android-nixpkgs.url = "github:HPRIOR/android-nixpkgs/516bd59caa6883d1a5dad0538af03a1f521e7764";
  };

  outputs = { self, nixpkgs, custom_nixpkgs, android-nixpkgs, ... }:
    let
      system = "x86_64-linux";
    in
    {
      devShells."${system}".default =
        let
          pkgs = import nixpkgs {
            inherit system;
          };
          custom_pkgs = import custom_nixpkgs {
            system = system;
          };
          android-sdk = (android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
            cmdline-tools-latest
            build-tools-34-0-0
            platform-tools
            platforms-android-34
          ]));
        in
        pkgs.mkShell {
          packages = with pkgs; [
            jdk21_headless
            curl.out
            (custom_pkgs.kotlin-native-toolchain-env.override { x86_64 = true; aarch64 = true; msys2 = true; })
            android-sdk

            # Runtime
            patchelf
            #glibc
            #glibc_multi
            #libgcc.lib
          ];

          JAVA_HOME = "${pkgs.jdk21_headless}/lib/openjdk";
          KOTLIN_BINARY_PATCH_COMMAND = "patchkotlinbinary";

          shellHook = ''
            # Add NIX_LDFLAGS to LD_LIBRARY_PATH
            lib_paths=($(echo $NIX_LDFLAGS | grep -oP '(?<=-rpath\s| -L)[^ ]+'))
            lib_paths_str=$(IFS=:; echo "''${lib_paths[*]}")
            export LD_LIBRARY_PATH="$lib_paths_str:$LD_LIBRARY_PATH"

            # Add glibc and glibc_multi to C_INCLUDE_PATH
            export C_INCLUDE_PATH="${pkgs.glibc.dev}/include:${pkgs.glibc_multi.dev}/include:$C_INCLUDE_PATH"

            export KONAN_DATA_DIR=$(pwd)/.konan

            mkdir -p $KONAN_DATA_DIR
            cp -asfT ${custom_pkgs.kotlin-native-toolchain-env} $KONAN_DATA_DIR
            chmod -R u+w $KONAN_DATA_DIR

            mkdir $KONAN_DATA_DIR/bin
            export PATH="$KONAN_DATA_DIR/bin:$PATH"

            PATCH_KOTLIN_BINARY_SCRIPT="patchelf --set-interpreter \$(cat \$NIX_CC/nix-support/dynamic-linker) --set-rpath $KONAN_DATA_DIR/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot/lib64 \$1"
            echo "$PATCH_KOTLIN_BINARY_SCRIPT" > $KONAN_DATA_DIR/bin/$KOTLIN_BINARY_PATCH_COMMAND
            chmod +x $KONAN_DATA_DIR/bin/$KOTLIN_BINARY_PATCH_COMMAND

            chmod -R u+w $KONAN_DATA_DIR

            # I have no idea how else to do this short of modifying Ktor or Kotlin
            # Tried modifying Ktor, but didn't feel like fighting to get it to build
            ln -sf ${pkgs.curl.out}/lib/libcurl.so /usr/lib/x86_64-linux-gnu/libcurl.so
          '';
        };
    };
}

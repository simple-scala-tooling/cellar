{
  description = "Look up the public API of any JVM dependency from the terminal";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }:
    let
      version = "0.1.0-M7";

      # Per-platform release artifact metadata
      platforms = {
        x86_64-linux = {
          archive = "cellar-${version}-linux-x86_64.tar.gz";
          hash = "sha256-iQaZxMeE7Iu5vhaOFtOOZ5MCRBrE0O/DjtZUT6L6NpQ=";
        };
        aarch64-linux = {
          archive = "cellar-${version}-linux-aarch64.tar.gz";
          hash = "sha256-XabTzGAnQOdxIW9GxkCmi/CMT/KQE9ZEV67HAMqGmmc=";
        };
        x86_64-darwin = {
          archive = "cellar-${version}-macos-x86_64.tar.gz";
          hash = "sha256-QmJJQHAVP/mlF7Er09lYbrN4YfKwXnznvwnOmIxhVbE=";
        };
        aarch64-darwin = {
          archive = "cellar-${version}-macos-arm64.tar.gz";
          hash = "sha256-nsVVyD28xubN5KoGKZgInWdXWaIM3zo7X+nR25t5AUE=";
        };
      };

      eachSystem = nixpkgs.lib.genAttrs (builtins.attrNames platforms);
    in
    {
      packages = eachSystem (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          meta = platforms.${system};
          src = pkgs.fetchurl {
            url = "https://github.com/VirtusLab/cellar/releases/download/v${version}/${meta.archive}";
            hash = meta.hash;
          };
        in
        {
          default = pkgs.stdenv.mkDerivation {
            pname = "cellar";
            inherit version src;

            sourceRoot = ".";

            nativeBuildInputs = pkgs.lib.optionals pkgs.stdenv.hostPlatform.isLinux [
              pkgs.autoPatchelfHook
              pkgs.glibc
              pkgs.zlib
            ];

            unpackPhase = ''
              tar xzf $src
            '';

            installPhase = ''
              mkdir -p $out/bin
              cp cellar $out/bin/cellar
              chmod +x $out/bin/cellar
            '';

            meta = with pkgs.lib; {
              description = "Look up the public API of any JVM dependency from the terminal";
              homepage = "https://github.com/VirtusLab/cellar";
              license = licenses.mpl20;
              platforms = [ system ];
              mainProgram = "cellar";
            };
          };

          # Install a locally-built binary into the nix profile.
          # Usage: ./mill cli.nativeImage && nix profile install --impure .#dev
          dev = let
            binary = builtins.path {
              path = builtins.toPath "${builtins.getEnv "PWD"}/out/cli/nativeImage.dest/native-executable";
              name = "cellar-native-executable";
            };
          in pkgs.stdenv.mkDerivation {
            pname = "cellar";
            version = "dev";
            dontUnpack = true;

            installPhase = ''
              mkdir -p $out/bin
              cp ${binary} $out/bin/cellar
              chmod +x $out/bin/cellar
            '';

            meta = with pkgs.lib; {
              description = "Look up the public API of any JVM dependency from the terminal (dev build)";
              homepage = "https://github.com/VirtusLab/cellar";
              license = licenses.mpl20;
              platforms = [ system ];
              mainProgram = "cellar";
            };
          };
        }
      );

      overlays.default = final: prev: {
        cellar = self.packages.${final.system}.default;
      };
    };
}

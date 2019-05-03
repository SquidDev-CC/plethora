# let pkgs = import <nixpkgs> {};
# in pkgs.mkShell {
#   buildInputs = [
#     pkgs.ruby.devEnv
#     pkgs.nodejs
#     pkgs.libffi
#     pkgs.libxml2
#     pkgs.libxslt
#     pkgs.pkgconfig
#     pkgs.gnumake
#     pkgs.zlib
# 
#     pkgs.bundix
#   ];
# }

with (import <nixpkgs> {});
let
  gems = bundlerEnv {
    name = "your-package";
    inherit ruby;
    gemdir = ./.;
  };
in stdenv.mkDerivation {
  name = "plethora-docs";
  buildInputs = [gems ruby nodejs];
}

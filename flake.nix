{
  description = "OwnRide Quarkus application";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";

  outputs = { nixpkgs, ... }:
    let
      forAllSystems = nixpkgs.lib.genAttrs [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
      ];
    in
    {
      packages = forAllSystems (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
        in
        {
          default = pkgs.maven.buildMavenPackage {
            pname = "ownride";
            version = "0.1.0-SNAPSHOT";
            src = pkgs.lib.fileset.toSource {
              root = ./.;
              fileset = pkgs.lib.fileset.unions [ ./pom.xml ./src ];
            };
            mvnJdk = pkgs.jdk21_headless;
            mvnHash = "sha256-lMEYlDinejbvTyhvdZhWGTOWiIV3jkTVPa2jPffhnA8=";
            doCheck = false;

            nativeBuildInputs = [ pkgs.makeWrapper ];

            installPhase = ''
              runHook preInstall
              mkdir -p "$out/bin" "$out/share/ownride"
              cp -r target/quarkus-app/. "$out/share/ownride/"
              makeWrapper ${pkgs.jdk21_headless}/bin/java "$out/bin/ownride" --add-flags "-jar $out/share/ownride/quarkus-run.jar"
              runHook postInstall
            '';

            meta = {
              description = "Analyze and manage cycling activities from Garmin FIT files";
              homepage = "https://github.com/sw1ld/own-ride";
              license = pkgs.lib.licenses.gpl3Only;
              mainProgram = "ownride";
            };

          };
        });

      devShells = forAllSystems (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
        in
        {
          default = pkgs.mkShell {
            packages = [ pkgs.maven pkgs.jdk21_headless ];
            JAVA_HOME = "${pkgs.jdk21_headless}";
          };
        });
    };
}

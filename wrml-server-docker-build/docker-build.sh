
cp ../server/server-war/target/ROOT.war .

cp ../server/server-war/target/classes/wrml.json .

cp ../cli/target/wrml-cli.jar .

docker build -t wrml/wrml-server .
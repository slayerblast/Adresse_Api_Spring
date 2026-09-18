Apres avoir recuperer le projet
cmd -> mvn clean install
@Deprecier -> cmd pour lancer l'importation du csv ->  java -jar target/projet-0.0.1-SNAPSHOT.jar src/main/resources/adresses-79-2026-06-22.csv
@Deprecier -> si lancé avec intellij -> mettre en argument le chemin du csv exemple : src/main/resources/adresses-79-2026-06-22.csv
Maintenant l'argument correspondant au chemin du fichier doit être entrée dans swagger quand on veut lancer un batch
NE PAS OUBLIER AUSSI DE SUPPRIMER LE .gitkeep dans \data\csvFile
Veuillez créer une database avec le nom "adresse"
Pour lancer les test, lancer un premier traitement en avec un fichier pas trop lours pour gagner du temps
Pour lancer un traitement, mettez un fichier csv BAN dans le dossier data/csvFile et allez dans swagger -> batch-controller -> api/jobs/batch/lancer/{innerjob} et lancer avec importAdresseJob 
Lien api : http://localhost:8080/swagger-ui/index.html#
lien web : http://localhost:4200/

Bon run!!!

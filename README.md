# inpi-client
Client JAVA pour la version 2 des API INPI

La présentation de l'API est disponible [ici](https://data.inpi.fr/content/editorial/Acces_API_Entreprises)

Plus spécifiquement la documentation de l'API INPI est disponible [ici](https://www.inpi.fr/sites/default/files/documentation%20technique%20API%20formalit%C3%A9s_v3.0.pdf)
 
Et la documentation concernant les actes est disponible [ici](https://www.inpi.fr/sites/default/files/documentation%20technique%20API%20Actes%20v3.0_1.pdf)

Le dictionnaire des données et constantes utilisées par l'API est disponible [ici](https://www.inpi.fr/sites/default/files/Dictionnaire_de_donnees_INPI_2024_11_06_0.xlsx)

## Environnement de développement

1. Installer VS Code avec les extensions Java
2. Installer gnupg pour pouvoir publier une nouvelle version de la librairie sur les repos Maven : https://gpg4win.org/download.html
3. Importer dans Kleopatra le certificat de signature des binaires (clé publique et clé privée / secret)
4. Créer un access token depuis https://s01.oss.sonatype.org/#profile;User%20Token et le paramétrer dans "C:\Users\[username]]\.m2\settings.xml", pour le server avec l'id ossrh
5. Exécuter "./deploy.ps1", ou "mvn clean deploy"

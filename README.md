[![ServerNet](https://i.imgur.com/EwBly0J.png)](https://servernet.pl/)
# venAuth Opis
Jest to dodatek do serwerów minecraft opartych na serwerze spigot (i pochodnych), który umożliwia autoryzację administratora za pomocą smartfona

# Spis treści
1. [Moduły](#Moduły)
2. [Instalacja](#Instalacja)
3. [Licencja](#Licencja)
 
  
# Moduły  <a name="Moduły"></a>
  - [Plugin](../../tree/plugin) - moduł służący do komunikacji między serwerem minecraft a serwerem autoryzacji.
  - [Serwer](../../tree/server) - moduł służący do komunikacji między serwerem minecraft, serwerem bazy danych oraz użytkownikami smartfonów.
  - [Plugin](../../tree/newApp) - moduł służący do zarządzania uprawnieniami dostępu do serwera. Dostepny również w [sklepie play](https://play.google.com/store/apps/details?id=ventan.app.venauth). Minimalna wersja androida 6.0 Marshmellow

# Instalacja <a name="Instalacja"></a>
###### Szczegółowe omówienie instalacji poszczególnych elementów, oraz filmik na YT znajduje się w w [wiki](../../wiki)
  1. Zainstaluj na serwerze bazę danych MYSQL
  2. Stwórz bazę danych o nazwie "venAuth" oraz przejdź do niej
  3. Wykonaj skrypt venAuth.sql w bazie danych
  4. Zainstaluj javę na maszynie na której będzie stał serwer autoryzacji (min. wersja 8)
  5. Wrzuć plik venAuthSrv.jar na maszynę na której będzie stał serwer autoryzacji.
  6. W konsoli przejdź do folderu w którym jest plik serwera.
  7. Włącz sewrer poleceniem `java -jar venAuthSrv.jar`. Serwer powinien stworzyć plik konfiguracyjny. Uzupełnij go koniecznymi danymi, wymyśl hasło.
  8. Odblokuj port na maszynie którym stoi serwer autoryzacji.
  9. Włacz serwer autoryzacji.
  10. Przejdź do folderu w którym znajdują się pluginy minecraft, wrzuć tam plik venAuthPlugin.jar po czym przeładuj serwer minecraft.
  11. Po przeładoawniu w folderze plugins zostanie stworzony folder venAuth, a w nim będzie plik konfiguracyjny config.yml. Uzupełnij go koniecznymi danymi, po czym przeładuj serwer minecraft.
  12. Zainstaluj aplikację ze sklepu play i zaloguj się do serwer podając IP:port i hasło.
# Licencja <a name="Licencja"></a>

 **MIT License**

cd sipua
javac -d ./build main.java
cd build
jar cfm ../../sipua.jar ../MANIFEST.mf *
cd ../../webphone
javac -d ./build WebUI.java
cd build
jar cfm ../../Webphone.jar ../MANIFEST.mf *
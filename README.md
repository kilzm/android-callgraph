# android-callgraph

Using [proguard-core](https://github.com/Guardsquare/proguard-core) to build the callgraph of an Android Package (apk).
Also print all calls to startActivity as well as paramters to System.out.println and methods from class [android.util.Log](https://developer.android.com/reference/android/util/Log).

## Usage

Run the program with gradle
```bash
gradle run --args /path/to/my/jdk /path/to/my/sdk /path/to/myapp.apk [-a] [-p] [-l]
```

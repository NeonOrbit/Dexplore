# Dexplore

A dex analyzing library for finding obfuscated classes and methods at runtime.

### Description
With the help of dexplore, you can develop portable apps (eg: xposed module) for any obfuscated codes.
It will automatically find obfuscated classes/methods dynamically based on your provided query.  
Additionally, the command line tool can make your reverse-engineering work easier.
It can also de-compile apps partially, which is faster and less resource extensive.

**Supported types:** apk, dex, odex, oat, zip


## Usage
Check latest version of dexplore from the release page.

### Library
Library dependency

```Groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation 'io.github.neonorbit:dexplore:1.4.3'
}
```

### CommandLine
Command line tool for static analysis and app de-compilation

```Shell
java -jar Dexplore.jar --help
```

### Xposed Sample
A sample for xposed module. Read wiki page for detailed explanation.

```java
public class XposedModule implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.example.app")) return;

        // Create a class filter to find your target class
        ClassFilter classFilter = new ClassFilter.Builder()
                .setReferenceTypes(ReferenceTypes.builder().addString().build())
                .setReferenceFilter(pool ->
                        pool.contains("a unique string inside the class")
                ).build();

        // Create a method filter to find your target method from the class
        MethodFilter methodFilter = new MethodFilter.Builder()
                .setReferenceTypes(ReferenceTypes.builder().addString().build())
                .setReferenceFilter(pool ->
                        pool.contains("a unique string inside the method")
                ).setParamSize(3)   // consider it has 3 parameters
                .setModifiers(Modifier.PUBLIC)   // and it's a public method
                .build();

        // Load the base apk into Dexplore
        Dexplore dexplore = DexFactory.load(lpparam.appInfo.sourceDir);

        // Perform a dex search
        MethodData result = dexplore.findMethod(DexFilter.MATCH_ALL, classFilter, methodFilter);

        // Load the actual method
        Method method = result.loadMethod(lpparam.classLoader);

        // Xposed hook
        XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(null));
    }
}
```

### Usage details

- Check [JavaDocs](https://neonorbit.github.io/dexplore/javadoc) for API overview.  
- Check [WikiPage](https://github.com/NeonOrbit/Dexplore/wiki) for detailed explanation and examples.


## Support
For help: [XDA-Thread](https://forum.xda-developers.com/t/4477899)  
You can find sample projects from the xda-thread.


## License

```
Copyright (C) 2022 NeonOrbit

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

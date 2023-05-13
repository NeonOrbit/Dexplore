# Dexplore

A dex analyzing library for finding obfuscated classes and methods at runtime.

### Description
Dexplore is a java library designed for analyzing and finding obfuscated classes and methods.
The library can automatically locate obfuscated classes and methods based on specific queries,
enabling developers to create portable apps, such as xposed modules, for any obfuscated codebase.  
Additionally, Dexplore offers a command-line tool that provides the capability to perform static analysis and app de-compilation.
The tool is also capable of extracting specific class files and resources, resulting in a faster and less resource-intensive process.

**Supported types:** apk, dex, odex, oat, zip.  
**Supported inputs:** file path, byte buffer (in-memory dex).


## Usage
Please check the latest version of dexplore from the release page.

### Library
Library dependency

```Groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation 'io.github.neonorbit:dexplore:1.4.5'
}
```

### CommandLine
Command line tool for static analysis and app de-compilation

```Shell
java -jar Dexplore.jar --help
```

### Xposed Sample
A sample for xposed module.  
Please refer to the wiki page for detailed explanation.

```java
public class XposedModule implements IXposedHookLoadPackage {
  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
    if (!lpparam.packageName.equals("com.example.app")) return;

    // Create a class filter to find your target class
    ClassFilter classFilter = new ClassFilter.Builder()
            .setReferenceTypes(ReferenceTypes.STRINGS_ONLY)
            .setReferenceFilter(pool ->
                    pool.contains("a unique string inside the class")
            ).build();

    // Create a method filter to find your target method from the class
    MethodFilter methodFilter = new MethodFilter.Builder()
            .setReferenceTypes(ReferenceTypes.STRINGS_ONLY)
            .setReferenceFilter(pool ->
                    pool.contains("a unique string inside the method")
            ).setParamSize(2)   // suppose it has 3 parameters
            .setModifiers(Modifier.PUBLIC)   // and it's a public method
            .build();

    // Load the base apk into Dexplore
    Dexplore dexplore = DexFactory.load(lpparam.appInfo.sourceDir);

    // Perform a dex search
    MethodData result = dexplore.findMethod(classFilter, methodFilter);

    // Load the actual method
    Method method = result.loadMethod(lpparam.classLoader);

    // Hook with Xposed
    XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));

    // ------------------ Extra ------------------
    // Please don't forget to save the result in Preferences.
    preferences.edit()
            .putString("targetMethod", result.serialize())  // serialize and save the result
            .putLong("appVersion", pkgInfo.getLongVersionCode()) // version code (NOT version name)
            .apply();
    // Use the saved result until the app LongVersionCode changes.
    String saved = preferences.getString("targetMethod", null);
    MethodData retrieved = MethodData.deserialize(saved);  // Deserialize

    // Please refer to the wiki page for a detailed explanation.
  }
}
```

### Usage details

- Check [JavaDocs](https://neonorbit.github.io/dexplore/javadoc) for API overview.  
- Check [WikiPage](https://github.com/NeonOrbit/Dexplore/wiki) for detailed explanation and examples.


## Support
For help: [XDA-Thread](https://forum.xda-developers.com/t/4477899)  
Sample projects can be found on the XDA thread.


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

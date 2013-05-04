---
layout: index
title: Wink.JS > Compilation
---

## Compilation

### I use maven to compile the source code

Just retrieve the source of the project

```bash
git clone https://github.com/nioto/wink.js.git
```

 and run

```bash
mvn package
```

The jar will be in the _target/_  directory

#### Note :
The pom.xml use Wink v.1.3.0, if you want to use Wink v.1.2.1-incubating  just replace, in pom.xml, the part :

```xml
<wink.version>1.3.0</wink.version>
```
by

```xml
<wink.version>1.2.1-incubating</wink.version>
```



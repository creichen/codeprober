# PASTA - Probe AST Attributes

A prototype implementation of property probes.

⚠️ In active development ⚠️

## Building - Client

The client is built with TypeScript. Do the following to generate the JavaScript files:

```sh
cd client/ts
npm install
npm run bw
```

Where 'bw' stands for 'build --watch'.
All HTML, JavaScript and CSS files should now be available in the `client/public` directory.

## Build - Server

The server is written in Java and has primarily been developed in Eclipse, and there are `.project` & `.classpath` files in the repository that should let you import the project into your own Eclipse instance.
That said, you can also build with the command line. To do so, do the following:

```sh
cd server
./build.sh
```

## Running

In general, the tool tries to print helpful messages when needed, so you shouldn't have to read this file to get started.
Try starting the tool without any arguments and it will tell you the following:
```
Usage: java -jar pasta-server.jar path/to/your/analyzer-or-compiler.jar [args-to-forward-to-compiler-on-each-request]
```

Your tool needs to assign to a `public static Object DrAST_root_node` in the main function.
This is the object that the `pasta-server.jar` will use as entry point.
For information about why it is called "DrAST", see https://bitbucket.org/jastadd/drast/src/master/.
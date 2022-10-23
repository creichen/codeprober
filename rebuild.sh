#! /bin/bash

(cd client/ts; npm install; npm run build)
(cd server ; ./build.sh)



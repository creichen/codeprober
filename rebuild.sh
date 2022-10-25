#! /bin/bash

(cd client/ts; npm install; npm run build) || exit 1
(cd server ; ./build.sh)



#!/bin/bash
while true; do
  python3 -m http.server 3000 --directory web_portal
  sleep 1
done

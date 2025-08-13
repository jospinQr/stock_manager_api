FROM ubuntu:latest
LABEL authors="jospi"

ENTRYPOINT ["top", "-b"]
FROM vanilla:latest

LABEL version="1.0"
LABEL description="Install any package using SDKMan"



RUN ["mkdir", "-p", "/apps/home"] 
RUN ls -la /apps/home
COPY . /apps/home
WORKDIR /apps/home
RUN apt-get update && apt-get install -y python-argparse libglib2.0-0
RUN pip2 install -r requirements.txt

EXPOSE 5005
EXPOSE 8080
ENTRYPOINT ["./docker-entry.sh"]



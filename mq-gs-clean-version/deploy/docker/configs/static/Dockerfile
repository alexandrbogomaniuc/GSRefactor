FROM nginx:1.13.10

COPY nginx.conf /etc/nginx/nginx.conf
COPY games /etc/nginx/sites-enabled/games

RUN echo "deb [trusted=yes] http://archive.debian.org/debian stretch main contrib non-free" > /etc/apt/sources.list

RUN rm /etc/nginx/conf.d/default.conf
RUN chmod 777 /etc/nginx/nginx.conf && chmod -R 777 /etc/nginx/sites-enabled
RUN useradd uploader-stat && echo uploader-stat:Squ2pKvGUmJjsecX | chpasswd
RUN apt-get -o Acquire::Check-Valid-Until=false update
RUN apt-get install -y --allow-unauthenticated openssh-server
RUN echo "Port 222" >> /etc/ssh/sshd_config && echo "AllowUsers uploader-stat" >> /etc/ssh/sshd_config
RUN update-rc.d ssh defaults

CMD service ssh start && nginx -g "daemon off;"

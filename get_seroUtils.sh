#!/bin/bash
hashToCheck=3a4efdf56d6309a369a39e251ce3997631d888fca7a64dfe94a13587a2953ced
function getSeroUtilsFromSourceforge() {
    curl https://netix.dl.sourceforge.net/project/scadabr/Dependency/seroUtils.jar --output seroUtils.jar
    echo $(sha256sum seroUtils.jar | head -c 64)
}

function getSeroUtilsFromGitHub() {
    curl -LJO https://github.com/SCADA-LTS/SeroUtils/releases/download/1.0.0/seroUtils.jar > 'seroUtils.jar'
    echo $(sha256sum seroUtils.jar | head -c 64)
}

function getSeroUtilsFromLocal() {
    if [ -e './WebContent/WEB-INF/lib/seroUtils.jar' ] ; then
       echo $(sha256sum './WebContent/WEB-INF/lib/seroUtils.jar' | head -c 64)
    else
       echo -12312414
    fi
}

function checkSeroUtils() {
    if [ "$hashToCheck" = "$1" ] ;then
        true
    else
        false
    fi
}

function isSeroUtils() {
    if checkSeroUtils $(getSeroUtilsFromLocal) ;then
       true
    elif $(checkSeroUtils $(getSeroUtilsFromSourceforge)) || $(checkSeroUtils $(getSeroUtilsFromGitHub)) ;then
       mv -f seroUtils.jar ./WebContent/WEB-INF/lib/
       true
    else
       rm seroUtils.jar
       false
    fi
}

i=0;
end=3;
while : ; do
    i=$(( i + 1 ))
    [ "$i" -lt "$end" ] && ! isSeroUtils || break
done

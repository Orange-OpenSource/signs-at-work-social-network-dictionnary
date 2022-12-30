MVN=mvn ${MAVEN_CLI_OPTS} ${MAVEN_OPTS}
VERSION=`git describe`

# Used to build debian package
# here we consider that artifacts are already built (to speed up continuous integration)
# so we do nothing here
build:

# Build debian package
deb:
	dch -v ${VERSION} "git update, version ${VERSION}"
	bash .dh_build.sh
	mv ../signsatwork*.deb .

# Debian package install
install:
	rm -rf usr && mkdir -p usr/share/java/signsatwork
	cp app/target/signs-at-work-*.jar usr/share/java/signsatwork/signsatwork.jar
	rm -rf etc && mkdir -p etc/systemd/system/ && cp debian/signsatwork.service etc/systemd/system/ && mkdir -p etc/signsatwork && cp application-work.yml etc/signsatwork

clean:
	debclean

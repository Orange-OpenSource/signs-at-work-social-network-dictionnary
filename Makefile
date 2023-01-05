MVN=mvn ${MAVEN_CLI_OPTS} ${MAVEN_OPTS}
VERSION=`git describe`
INSTANCE=$(instance)

# Used to build debian package
# here we consider that artifacts are already built (to speed up continuous integration)
# so we do nothing here
build:

# Build debian package
deb:
	rm -rf debian && mkdir -p debian && cp -a debian_$(INSTANCE)/. debian
	dch -v ${VERSION} "git update, version ${VERSION}"
	rm -rf usr && mkdir -p usr/share/java/signsat$(INSTANCE)
	cp app/target/signs-at-work-*.jar usr/share/java/signsat$(INSTANCE)/signsat$(INSTANCE).jar
	rm -rf etc && mkdir -p etc/systemd/system/ && cp debian/signsat$(INSTANCE).service etc/systemd/system/ && mkdir -p etc/signsat$(INSTANCE) && cp application-$(INSTANCE).yml etc/signsat$(INSTANCE)
	bash .dh_build.sh
	mv ../signsat$(INSTANCE)*.deb .



clean:
	debclean

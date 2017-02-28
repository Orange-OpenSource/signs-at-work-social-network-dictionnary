We use the github release feature, for instance see here: https://github.com/Orange-OpenSource/signs-at-work-social-network-dictionnary/releases/tag/1.0.2

The idea is to try to automate the release process on github, thanks to `git tags` ; to do so we use the process described below

## Git tags

When you want to **tag** a version with git, you will add a tag to current branch commit (doc [here](https://git-scm.com/book/en/v2/Git-Basics-Tagging)).

- to list current tags: `git tags -l`
- to create a tag: `git tag -a "1.0.3"`, followed by `git push --tags`

So if last tag was `1.0.2` (found with `git tag -l` ) and you want to create a new version `1.0.3`, you will do:
- `git tag -a "1.0.3"`
- `git push --tags`

Basically you may create tags only on your master branch

## Travis will help us
The travis build (`.travis.yml`) is configured to create a release when it is building a tagged commit.
If the build is successful, it will then push the build artefact to github automatically.

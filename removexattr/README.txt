Tool to remove all xattrs on a file/directory/symlink.

Can be used recursively:
find . -print0 | xargs -0 removexattr

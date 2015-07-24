#include <sys/types.h>
#include <sys/stat.h>
#include <sys/xattr.h>
#include <fcntl.h>
#include <alloca.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char ** argv) {
  const size_t buffersize = 128 * 1014;
  char * const listbuffer = (char*)alloca(buffersize);

  for (int i = 1; i < argc; i++) {
    const char * const pathname = argv[i];

    const ssize_t size = llistxattr(pathname, listbuffer, buffersize);
    if (size == -1) {
      fprintf(stderr, "Failed to list extended attributes on: %s\n", pathname);
      perror("llistxattr()");
      continue;
    }

    for (int j = 0; j < size; j += strlen(listbuffer + j) + 1) {
      const char * const attrname = listbuffer + j;
      const int removeresult = lremovexattr(pathname, attrname);
      if (removeresult == -1) {
        fprintf(stderr, "Failed to remove attr on file: %s, %s\n", pathname, attrname);
        perror("lremoveattr()");
      } else {
        fprintf(stdout, "Removed attr: %s: %s\n", pathname, attrname);
      }
    }
  }


  return 0;
}


# PC-IDE

PC-IDE is an IDE intended for use in learning environments (such as schools). It interprets pseudocode (that isn't
pseudocode anymore because it became an actual language, but we can forget that) in a server, called the "interpreter".

The interpreter is a server because, since this is made for use in learning environments, several IDEs will be open all
at once, and having a central interpreter lightens the load on the client side (that may be a computer that can barely
open gnome calculator, as a lot of schools use), and makes it possible for the instructor to have statistics about his
students' performance without having to walk at every computer.

## Tasks

Having the interpreter in a server enables "tasks" to be distributed to clients very easily. A "task" is a set of tests
that an application must pass, that is created in the server and distributed to all clients that connect to it.

The concept of a task makes it much easier for students to debug their application (since they know exactly where their
application is failing), and for the instructors, that have exactly how many and what of the tests a certain student's
application passed and failed, then being able to give help faster and give grades more easily.
# crashbox
Work-in-progress project that should become a CI system some day.

Not much functionality of a CI system has been implemented
yet. Nevertheless this project already showcases some interesting
features on their own:

- server using Akka HTTP, Twirl and Slick
- user interface using ScalaJS
- API client in Scala Native 
- JSON format derivation for seamless communication between all components

## Layout
```
├── cbx    // native command line client
├── server // main server
├── shared // shared code among all components
└── ui     // scalajs user interface
```

...

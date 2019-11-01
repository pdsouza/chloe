# Chloe

[![Clojars Project](https://img.shields.io/clojars/v/chloe.svg)](https://clojars.org/chloe)

A simple, composable static site generator for Clojure.

**chloe is in the very early stages right now and documentation / examples are sparse—this will improve soon!**

## Concepts

### Content Types

#### Page

A Clojure file that renders HTML. Pages can depend on partial resources.

This is where the real power of Chloe lies since you can generate your HTML using Clojure itself and all the power it provides—no need to squeeze yourself into a templating language's confines.

#### Partial

A standalone piece of content that will likely be transformed, laid out, etc. like a markdown blog post.

#### Asset

A pre-rendered file that bypasses processing.

## Usage

Chloe consists of a lean set of sharp functions that make building static sites in Clojure a joy. Here are some common ways to build sites with Chloe.

### Structure

Websites map very nicely to directory structures, so let's create one that represents your site.

Here's a sample layout:

```
resources/
  partials/
    post/
      chloe-is-awesome.md
      chloe-is-cool.md
  public/
    main.css
    images/
      chloe-architecture.jpg
src/
  my-site/
    page/
      about.clj
      index.clj
    core.clj
    layouts.clj
```

In this layout, partial content (simple text-focused content that will likely be transformed, laid out, etc. like markdown blog posts for example) is placed in `resources/partials` and assets (stuff that will be reflected exactly over to your site without any intermediate transforms) are placed in `resources/public`.

Notice how this structure is exactly the same as a standard Clojure project. That means you can work on your website using all the Clojure tooling that you know and love, like Leiningen.

### Build

Now, let's tap into Chloe to build your site:

```clojure
(ns preetam.core
  (:require [chloe.core :as c]
            [chloe.plugin.layout :refer [layout]]
            [preetam.layouts :as layouts]))

(def site
  {:url "https://preetam.io"
   :title "Preetam D'Souza"
   :asset-path "resources/public"
   :project-name "preetam"
   :export-path "_site"
   :plugins [[layout {"/post/.+" layouts/post}]]})

(def ring-handler (c/ring-serve site))
```

As you can see, building your site is as simple as declaring your site structure in a Clojure map.

### Export

When you want to export your site, just use `chloe/export`:

```clojure
(defn export [] (c/export site))
```

### Develop Live

For live development, you can set up a Ring handler by passing your build function to `chloe/ring-serve`:

```clojure
(def ring-handler (c/ring-serve site))
```

Run a development server with:

    $ lein ring server

## Examples

The following sites are built with Chloe:

 * [preetam.io](https://preetam.io/)

## Prior Art

Thanks to:

 * [Metalsmith](https://github.com/segmentio/metalsmith) for the insight that static site generators are simply a sequence of pluggable transformations applied to site content
 * [Stasis](https://github.com/magnars/stasis) for the observation that coding in Clojure is more fun than learning and editing framework-specific configuration
 * [perun](https://github.com/hashobject/perun) for early inspiration on mapping Metalsmith's design to Clojure

## License

Copyright © 2019 Preetam J. D'Souza

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

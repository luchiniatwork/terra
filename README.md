# Terra

Terra lets you write Terraform configurations using pure Clojure.

## Motivation

1. Have the full power of Clojure at your fingertips when defining Terraform configurations.
2. Do you need to iterate through something? Or maybe you need to automate part of the configuration creation? Just use the tools you are used to.
3. No need to learn a separate configuration language in details. A simple Clojure EDN will do.

## Table of Contents

* [Getting Started](#getting-started)
* [Usage](#usage)
* [Advanced](#advanced)
* [Bugs](#bugs)
* [Help!](#help)

## Getting Started

Add the following dependency to your `project.clj` file:

[![Clojars Project](http://clojars.org/luchiniatwork/terra/latest-version.svg)](http://clojars.org/luchiniatwork/terra)

Add the following plugin to your `:plugins` in your `project.clk` file:

[![Clojars Project](http://clojars.org/luchiniatwork/lein-terra/latest-version.svg)](http://clojars.org/luchiniatwork/lein-terra)

## Usage

Create a namespace in your project and require `terra.core` on your stateful component:

```clojure
(ns my-project.infrastructure.core
  (:require [terra.core :as terra :refer [defterra $]))
```

Notice we are also referring the macros `defterra` and `$`.

We are going to create an `example_ec2` definition using `defterra`. If you have done any 
Terraform before, this should be familiar to you.

```clojure
(defterra example_ec2 {:provider {:aws {:region :us-east-1}}
                       :resource {:aws_instance {:example {:ami :ami-2757f631
                                                           :instance_type :t2.micro}}}})
```

According to this definiation, `example_ec2` uses AWS in the us-east-1 region. It is a t2.micro
instance using ami-2757f631. We have used keywords but strings also work if you prefer.

The next step is creating a `handler` function for trigering the generation of your Terraform
file. Usually you just want to call `terra/generate` in it.

```clojure
(defn handler []
  (terra/generate))
```

Then add the following to your `project.clj`:

```clojure
:terra {:handler my-project.infrastructure.core/handler}
```

Head to your terminal and type:

```
$ lein terra
```

You should see:

```
Terra: generating...
Terra: terraform/my-project.infrastructure.core/example_ec2.tf.json
Terra: done!
```

You can now `plan` and then `apply` your Terraform configuration by:

```
$ cd terraform/my-project.infrastructure.core
$ terraform plan
...
$ terraform apply
...
```

Since the terraform files are always generated, it is recommended that you add the
`terraform` directory to your `.gitignore`.

## Advanced

The `$` macro is a small utility helper to write Terraform interpolations in a more 
Clojure-like way.

For instance, in order to interpolate a variable, simply use `:count ($ var.count)`

If you want the entry `:us-east-1 from the list `var.amis`, simply use 
`:ami ($ (:us-east-1 var.amis))`

The traditional `get` also works: `:ami ($ (get var.amis :us-east-1))`

This also works for indexes of lists: `:ami ($ (get var.amis 0))`

Last but not least, you can specify a call to Terraform's functions as if they were 
Clojure functions with: `:file ($ (file "path.txt"))`

## Bugs

If you find a bug, submit a [Github issue](https://github.com/luchiniatwork/terra/issues).

## Help

This project is looking for team members who can help this project succeed!
If you are interested in becoming a team member please open an issue.

## License

Copyright © 2017 Tiago Luchini

Distributed under the MIT License.

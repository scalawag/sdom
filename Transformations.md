The result of almost any node query (except for a Document) can be transformed.  This means that you can apply a partial function to each node in a result set and to create a new Document with the transformed nodes replacing the originals.  The output of the function must be an Iterable of the same type as the input of the function.

# transform

When you want to create a new document from an old document, use transform with selector.

    import org.scalawag.sdom._

    val xml = Document(
      <a>
        <b>
          <c>Foo</c>
          <c>Bar</c>
        </b>
        <c>Baz</c>
        <c>
          <d>Fizz</d>
          <d>Buzz</d>
        </c>
      </a>
    )

    // Double the children of all "c" elements in the document
    ( xml \\ "c" ) transform { case e:ElementSpec => Iterable(e.copy(children = e.children ++ e.children)) }

returns

    Document(
      <a>
        <b>
          <c>FooFoo</c>
          <c>BarBar</c>
        </b>
        <c>BazBaz</c>
        <c>
          <d>Fizz</d>
          <d>Buzz</d>
          <d>Fizz</d>
          <d>Buzz</d>
        </c>
      </a>
    )

Transformed documents maintain the comments, so that you can safely use it to manipulate configuration files.

    import org.scalawag.sdom._

    val xml = Document(
      <config>
        <!-- Here's a comment about the delay. -->
        <delay>10s</delay>
      </config>
    )

    ( xml \ "config" \ "delay" ) transform mapChildren( _ => Iterable("20s") )

returns

    <config>
      <!-- Here's a comment about the delay. -->
      <delay>20s</delay>
    </config>

You can even transform the comments themselves:

    // Note that we're selecting all of the nodes in the document but the partial function only manipulates comments.
    ( xml \\ node ) transform { case c:CommentSpec => Iterable(c.copy(text = c.text.replaceAll("about","ABOUT"))) }

produces

    <config>
      <!-- Here's a comment ABOUT the delay. -->
      <delay>10s</delay>
    </config>

Of course, it works with any kind of selector.

    import org.scalawag.sdom._

    val xml = Document(<a><b id="1">A</b><b id="2">A</b><b id="3">A</b></a>)

    // remove is a predefined partial function that removes whatever was selected from the document.
    ( xml % "//b[@id>=2]" ) transform remove

    // -> Document(<a><b id="1">A</b></a>)

Transformations expect that you replace any node with a compatible node in the tree.  This means that you can replace any type of ChildSpec with zero or more of any other ChildSpec.

    import org.scalawag.sdom._

    val xml = Document(<a><b id="1">A</b><b id="2">A</b><b id="3">A</b></a>)

    // Replace all selected elements with their children
    ( xml % "//b[@id>=2]" ) transform { case e:ElementSpec => e.children }

    // -> Document(<a><b id="1">A</b>AA</a>)

# transformNodes TODO - REMOVE THIS FROM THE CODE, IT'S USELESS

transformNodes is useful if you want to transform some nodes but the result is not a document for whatever reason.  `transform` is actually just a specialization of this method that assumes the resulting Iterable contains exactly one DocumentSpec.

    import org.scalawag.sdom._

    val xml:ElementSpec = <a><b id="1">A</b><b id="2">A</b><b id="3">A</b></a>

    ( xml \ "a" ) transform { n => Iterable(n,n) }

    // -> Document(<a><b id="1">A</b></a>)

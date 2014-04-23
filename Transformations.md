The result of almost any node query (except for a Document) can be transformed.  This means that you can apply a partial function to each node in a result set and to create a new Document with the transformed nodes replacing the originals.  The output of the function must be an Iterable of the same type as the input of the function.

# Transform

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

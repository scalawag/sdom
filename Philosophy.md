TODO: EDIT

Create a specification and then build a selectable/queryable/serializable document from that.

Namespace prefixes are not significant.  They can aid readability but they are not semantically significant.

Elements and attributes have expanded names that are important.  Their QNames are replaceable.  
Let the computer do the work for you of making sure that your prefixes are consistent.

Example of using a new prefixx and having SDOM rewrite it so that there's only one declaration in the final document.
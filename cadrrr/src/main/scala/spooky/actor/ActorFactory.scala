package spooky.actor

import scala.annotation.tailrec
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList

private[actor] class ActorFactory(props: Props) {

  private val argTypes = props.arguments.map(_.getClass).toArray
  //    println(props)
  //    println(argTypes.length)
  //    argTypes.foreach(println(_))
  //    println(props.c.getConstructors.foreach(c => {
  //      c.getParameterTypes.map(_.getSimpleName).foreach(println(_))
  //      println("--" + c.getParameterCount)
  //    }))
  private val matchingConstructors = props.c.getConstructors.filter(c => {
    if (c.getParameterCount == props.arguments.length) {
        @tailrec
        def rec(search: Array[Class[_]], from: Array[Class[_]], index: Int = 0): Boolean = {
          if (search.length == index) {
            true
          } else if (search(index).isAssignableFrom(from(index))) {
            rec(search, from, index + 1)
          } else false
        }
      rec(c.getParameterTypes, argTypes)
    } else false
  })

  if (matchingConstructors.length == 0) {
    val strCNames = argTypes.map(_.getSimpleName).toList
    throw new RuntimeException(s"No matching constructor for class ${props.c} with parameters ${strCNames}")
  } else if (matchingConstructors.length > 1) {
    throw new RuntimeException(s"Ambigous constructor for class ${props.c}")
  }
  //      println(matchingConstructors(0))
  private val construct = matchingConstructors(0)

  def create: Actor = {
    if (construct.getParameterCount == 0) {
      construct.newInstance().asInstanceOf[Actor]
    } else construct.newInstance(props.arguments.map(_.asInstanceOf[AnyRef]): _*).asInstanceOf[Actor]
  }
}

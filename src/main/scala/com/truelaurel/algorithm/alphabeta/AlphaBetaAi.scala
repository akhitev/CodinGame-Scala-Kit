package com.truelaurel.algorithm.alphabeta

import com.truelaurel.algorithm.game._

import scala.annotation.tailrec

/**
  * @param heuristic must represent higher chance of success for state.nextPlayer
  */
case class AlphaBetaAi[S <: GameState[Boolean], M](rules: RulesFor2p[S, M],
                                                   heuristic: S => Double) {

  val MIN = Double.MinValue
  val MAX = Double.MaxValue

  def chooseMove(state: S, depth: Int): M = {
    val sorted = sortedMoves(state)
    best(sorted, MIN, MAX, state, depth).move.getOrElse(sorted.head)
  }


  def sortedMoves(state: S): Seq[M] = {
    rules.validMoves(state).sortBy(m => heuristic(rules.applyMove(state, m)))
  }

  def negamax(state: S,
              depth: Int,
              alpha: Double,
              beta: Double): Double = {
    val player = state.nextPlayer
    rules.outcome(state) match {
      case Wins(`player`) => MAX
      case Wins(_) => MIN
      case Draw => 0
      case Undecided =>
        val moves = sortedMoves(state)
        if (depth == 0 || moves.isEmpty) heuristic(state)
        else best(moves, alpha, beta, state, depth).score
    }
  }

  @tailrec
  final def best(moves: Seq[M],
                 alpha: Double,
                 beta: Double,
                 state: S,
                 depth: Int,
                 currentBest: Option[M] = None): ScoredMove = {
    if (beta > alpha && moves.nonEmpty) {
      val move = moves.head
      val nextState = rules.applyMove(state, move)
      val evaluation = -negamax(nextState, depth - 1, -beta, -alpha)
      val newBest = if (evaluation > alpha) Some(move) else currentBest
      best(moves.tail, alpha max evaluation, beta max evaluation, state, depth, newBest)
    } else ScoredMove(alpha, currentBest)
  }


  case class ScoredMove(score: Double, move: Option[M])

}

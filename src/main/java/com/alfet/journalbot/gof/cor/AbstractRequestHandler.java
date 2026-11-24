package com.alfet.journalbot.gof.cor;

/**
 * Abstract class implementing basic functionality for 'Chain of Responsibility'.
 * @see RequestHandler
 * @param <R> Type of the request.
 */
public abstract class AbstractRequestHandler<R> implements RequestHandler<R> {

  private RequestHandler<R> nextHandler;

  @Override
  public void setNext(RequestHandler<R> handler) {
    nextHandler = handler;
  }

  @Override
  public void handle(R request) {
    if (nextHandler != null) {
      nextHandler.handle(request);
    }
  }
}

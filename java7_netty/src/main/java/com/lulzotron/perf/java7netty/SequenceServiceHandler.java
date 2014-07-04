package com.lulzotron.perf.java7netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A handler for a Netty 4 implementation of the Sequence API (cf. <raml spec>
 * ).
 * </p>
 * 
 * <p>
 * Find business logic in {@link #route}, primary socket-managing code in
 * {@link #channelRead}.
 * </p>
 * 
 * 
 * @author mdye
 *
 */
public class SequenceServiceHandler extends ChannelInboundHandlerAdapter {

  static final Logger LOG = LoggerFactory
      .getLogger(SequenceServiceHandler.class);

  private static final HttpVersion SUPPORTED_HTTP_VER = HttpVersion.HTTP_1_1;

  private HttpRequest request;

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel
   * .ChannelHandlerContext, java.lang.Object)
   */
  @Override
  public void channelRead(ChannelHandlerContext context, Object message)
      throws URISyntaxException {

    // only care about query params so far, no need to inspect req content using
    // HttpMessage instance check
    if (message instanceof HttpRequest) {
      request = (HttpRequest) message;

      // the lamest routing, evar!
      HttpResponse response = route(context, request);

      // just write response, don't flush!
      if (OK != response.getStatus() || !HttpHeaders.isKeepAlive(request)) {
        context.write(response).addListener(ChannelFutureListener.CLOSE);
      } else {
        context.write(response, context.voidPromise());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.netty.channel.ChannelInboundHandlerAdapter#channelReadComplete(io.netty
   * .channel.ChannelHandlerContext)
   */
  @Override
  public void channelReadComplete(ChannelHandlerContext context) {
    // expensive operation, only do when passing req out of this handler
    context.flush();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.netty.channel.ChannelInboundHandlerAdapter#exceptionCaught(io.netty.
   * channel.ChannelHandlerContext, java.lang.Throwable)
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
    if (cause instanceof IOException) {
      LOG.error("IO interrupted, cf. debug log");
      LOG.debug("IO error", cause);
    } else {
      LOG.error("Unexpected error encountered", cause);
    }
    context.close();
  }

  /*
   * Route requests by path, delegate to a content generator
   */
  private HttpResponse route(ChannelHandlerContext context, HttpRequest request)
      throws URISyntaxException {
    String[] pathBits = new URI(request.getUri()).getPath().split("/");

    if (pathBits.length >= 4 && "api".equals(pathBits[1])) {
      switch (pathBits[2]) {
      case "count":
        try {
          return response(OK, doCount(pathBits[3]));
        } catch (Exception ex) {
          return response(BAD_REQUEST);
        }
      default:
        // let it fall through to 404
      }
    }

    return response(NOT_FOUND);
  }

  /*
   * Generate content for /count path
   */
  private String doCount(String param) {
    StringBuffer sb = new StringBuffer();

    // the slow and easy way
    int upTo = Integer.parseInt(param);
    for (int ix = 1; ix <= upTo; ix++) {
      sb.append(ix);

      if (ix < upTo) {
        sb.append(' ');
      }
    }

    return sb.toString();
  }

  /*
   * Generate response with optional content; handles response headers, writes
   * content
   */
  private HttpResponse response(HttpResponseStatus status, String content) {
    FullHttpResponse response;
    if (content != null) {
      response = new DefaultFullHttpResponse(SUPPORTED_HTTP_VER, status,
          Unpooled.unmodifiableBuffer(Unpooled.directBuffer().writeBytes(
              content.getBytes())), false);
      response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

      if (HttpHeaders.isKeepAlive(request)) {
        response.headers().set(CONTENT_LENGTH,
            response.content().readableBytes());
      }

    } else {
      response = new DefaultFullHttpResponse(SUPPORTED_HTTP_VER, status);
    }

    return response;
  }

  /*
   * Convenience method for content-less responses
   */
  private HttpResponse response(HttpResponseStatus status) {
    return response(status, null);
  }
}
package com.lulzotron.perf.java7netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceServiceInitializer extends
    ChannelInitializer<SocketChannel> {

  static final Logger LOG = LoggerFactory.getLogger(SequenceServiceInitializer.class);

  @Override
  protected void initChannel(SocketChannel channel)
  {
    ChannelPipeline pipeline = channel.pipeline();
    pipeline.addLast("codec", new HttpServerCodec());
    pipeline.addLast("handler", new SequenceServiceHandler());
  }
}

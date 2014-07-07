package com.lulzotron.perf.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lulzotron.perf.seq.SequenceGenerator;

public class SequenceServiceInitializer extends
    ChannelInitializer<SocketChannel> {

  static final Logger LOG = LoggerFactory.getLogger(SequenceServiceInitializer.class);
  
  private static final SequenceGenerator gen = new SequenceGenerator();

  @Override
  protected void initChannel(final SocketChannel channel)
  {
    ChannelPipeline pipeline = channel.pipeline();
    pipeline.addLast("codec", new HttpServerCodec());
    pipeline.addLast("handler", new SequenceServiceHandler(gen));
  }
}

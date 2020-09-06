/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.pilato.elasticsearch.crawler.fs;

import fr.pilato.elasticsearch.crawler.fs.framework.FsCrawlerUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * The processing pipeline that will be used if not overridden.
 *
 */
public class DefaultProcessingPipeline implements ProcessingPipeline {
    private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    protected TikaProcessor tika;
    protected EsIndexProcessor es;
    protected Config config;

    @Override
    public void processFile(FsCrawlerContext ctx) {
        logger.debug("Starting processing of file {}", ctx.getFullFilename());

        // Extracting content with Tika
        tika.process(ctx);

        // Index to es
        if (FsCrawlerUtil.isIndexable(ctx.getDoc().getContent(), config.getFsSettings().getFs().getFilters())) {
            es.process(ctx);
        } else {
            logger.debug("We ignore file [{}] because it does not match all the patterns {}", ctx.getFile().getName(),
                    config.getFsSettings().getFs().getFilters());
        }
    }

    @Override
    public void init(Config config) {
        this.config = config;
        tika = new TikaProcessor(config.getFsSettings(), config.getMessageDigest());
        es = new EsIndexProcessor(config.getFsSettings(), config.getEsClient());
    }
}

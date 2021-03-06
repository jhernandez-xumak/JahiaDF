/**
 * Danta Jahia Bundle
 * (danta.jahia)
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package danta.jahia.contextprocessors.lists;

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.exceptions.ProcessException;
import danta.jahia.templating.TemplateContentModel;
import danta.jahia.util.ResourceUtils;
import net.minidev.json.JSONObject;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.jahia.Constants.JAHIA_RENDER_CONTEXT;
import static danta.jahia.Constants.JAHIA_RESOURCE;

/**
 * This Context Processor adds to the content model a list of resolved paths'.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2017-08-11
 */
@Component
@Service
public class AddCuratedPageReferencesContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    public static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 20;

    @Override
    public Set<String> allOf() {
        return Sets.newHashSet(LIST_CATEGORY, CURATED_LIST_CATEGORY);
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
            RenderContext renderContext = (RenderContext) executionContext.get(JAHIA_RENDER_CONTEXT);

            // Add page path references
            if (contentModel.has(ITEM_LIST_KEY_NAME)) {
                Object pathRefs = contentModel.get(ITEM_LIST_KEY_NAME);

                Collection<Map<String, Object>> pathList = new ArrayList<>();
                String currentPage = contentModel.getAsString(PAGE + DOT + PATH);

                if (pathRefs instanceof Collection) {
                    for(Object pathRef : (Collection<Object>) pathRefs) {
                        JSONObject pagePath = new JSONObject();
                        String path = "";

                        if (pathRef instanceof String) {
                            path = ResourceUtils.getPathFromURL(pathRef.toString(), renderContext);
                        } else if (pathRef instanceof JSONObject) {
                            JSONObject a = (JSONObject) pathRef;
                            Object value = a.get(PATH);
                            if ( value != null ) {
                                path = ResourceUtils.getPathFromURL(value.toString(), renderContext);
                            }
                        }
                        if (!path.equals(currentPage)) {
                            pagePath.put(PATH, path);
                            pathList.add(pagePath);

                        } else if (!(contentModel.has(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY)
                                && contentModel.getAsString(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY).equals(TRUE))){
                            pagePath.put(IS_CURRENT_PAGE, true);
                            pagePath.put(PATH, path);
                            pathList.add(pagePath);
                        }
                    }
                }
                contentModel.set(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME, pathList);
            }

        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

}

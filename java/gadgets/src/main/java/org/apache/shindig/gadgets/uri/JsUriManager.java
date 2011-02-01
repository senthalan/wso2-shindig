/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.gadgets.uri;

import java.util.Collection;
import java.util.Collections;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.Gadget;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.RenderingContext;
import org.apache.shindig.gadgets.uri.UriCommon.Param;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Interface defining methods used to generate Uris for the /js servlet.
 */
public interface JsUriManager {
  /**
   * @param ctx The js parameters.
   * @return The uri for the externed javascript that includes all listed extern libraries.
   */
  Uri makeExternJsUri(JsUri ctx);

  /**
   * Processes the inbound URL, for use by serving code in determining which JS to serve
   * and with what caching properties.
   *
   * @param uri Generated extern JS Uri
   * @return Processed status of the provided Uri.
   */
  JsUri processExternJsUri(Uri uri) throws GadgetException;

  public static class JsUri extends ProxyUriBase {
    private final static Collection<String> EMPTY_COLL = ImmutableList.of();
    private final Collection<String> libs;
    private final Collection<String> loadedLibs;
    private final String onload;
    private boolean jsload;
    private boolean nohint;
    private final RenderingContext context;
    private final Uri origUri;

    public JsUri(UriStatus status, Uri origUri, Collection<String> libs, Collection<String> have) {
      super(status, origUri);
      if (origUri != null) {
        String param = origUri.getQueryParameter(Param.CONTAINER_MODE.getKey());
        this.context = RenderingContext.valueOfParam(param);
        this.jsload = "1".equals(origUri.getQueryParameter(Param.JSLOAD.getKey()));
        this.onload = origUri.getQueryParameter(Param.ONLOAD.getKey());
        this.nohint = "1".equals(origUri.getQueryParameter(Param.NO_HINT.getKey()));
      } else {
        this.context = RenderingContext.GADGET;
        this.jsload = false;
        this.onload = null;
        this.nohint = false;
      }
      this.libs = nonNullLibs(libs);
      this.loadedLibs = nonNullLibs(have);
      this.origUri = origUri;
    }

    public JsUri(UriStatus status) {
      this(status, null, EMPTY_COLL, EMPTY_COLL);
    }

    public JsUri(UriStatus status, Collection<String> libs, RenderingContext context,
                 String onload, boolean jsload, boolean nohint) {
      super(status, null);
      this.context = context;
      this.onload = onload;
      this.jsload = jsload;
      this.nohint = nohint;
      this.libs = libs;
      this.loadedLibs = EMPTY_COLL;
      this.origUri = null;
    }

    public JsUri(Gadget gadget, Collection<String> libs) {
      super(gadget);
      this.onload = null;
      this.jsload = false;
      this.nohint = false;
      this.context = RenderingContext.GADGET;
      this.libs = libs;
      this.loadedLibs = EMPTY_COLL;
      this.origUri = null;
    }

    public JsUri(Integer refresh, boolean debug, boolean noCache, String container, String gadget,
        Collection<String> libs, String onload, boolean jsload, boolean nohint, RenderingContext context, Uri origUri) {
      super(null, refresh, debug, noCache, container, gadget);
      this.onload = onload;
      this.jsload = jsload;
      this.nohint = nohint;
      this.context = context;
      this.libs = libs;
      this.loadedLibs = EMPTY_COLL;
      this.origUri = origUri;
    }
    
    public JsUri(JsUri origJsUri) {
      super(origJsUri.getStatus(), origJsUri.getRefresh(),
          origJsUri.isDebug(),
          origJsUri.isNoCache(),
          origJsUri.getContainer(),
          origJsUri.getGadget());
      this.libs = origJsUri.getLibs();
      this.loadedLibs = origJsUri.getLoadedLibs();
      this.onload = origJsUri.getOnload();
      this.jsload = origJsUri.isJsload();
      this.nohint = origJsUri.isNohint();
      this.context = origJsUri.getContext();
      this.origUri = origJsUri.getOrigUri();
    }

    public Collection<String> getLibs() {
      return libs;
    }
    
    public Collection<String> getLoadedLibs() {
      return loadedLibs;
    }
    
    private Collection<String> nonNullLibs(Collection<String> in) {
      in = in != null ? in : EMPTY_COLL;
      return Collections.unmodifiableCollection(in);
    }

    public RenderingContext getContext() {
      return context;
    }

    public String getOnload() {
      return onload;
    }

    public boolean isJsload() {
      return jsload;
    }
    
    public void setJsload(boolean jsload) {
      this.jsload = jsload;
    }
    
    public boolean isNohint() {
      return nohint;
    }
    
    public void setNohint(boolean nohint) {
      this.nohint = nohint;
    }
    
    public Uri getOrigUri() {
      return origUri;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof JsUri)) {
        return false;
      }
      JsUri objUri = (JsUri) obj;
      return (super.equals(obj)
          && Objects.equal(this.libs, objUri.libs)
          && Objects.equal(this.loadedLibs, objUri.loadedLibs)
          && Objects.equal(this.onload, objUri.onload)
          && Objects.equal(this.jsload, objUri.jsload)
          && Objects.equal(this.nohint, objUri.nohint)
          && Objects.equal(this.context, objUri.context))
          && Objects.equal(this.origUri, objUri.origUri);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(this.libs, this.loadedLibs, this.onload, this.jsload,
                              this.nohint, this.context, this.origUri);
    }
  }

  public interface Versioner {
    /**
     * @param gadgetUri Gadget for which extern Uri was generated.
     * @param container The container for this gadget.
     * @param extern Collection of libs externed.
     * @return Version string for the Uri.
     */
    String version(String gadgetUri, String container, Collection<String> extern);

    /**
     * @param gadgetUri Gadget for which extern Uri was generated.
     * @param container corresponding container for this gadget.
     * @param extern Collection of libs externed.
     * @param version Version string generated by the Versioner.
     * @return Validation status of the version.
     */
    UriStatus validate(String gadgetUri, String container, Collection<String> extern, String version);
  }
}

/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.technologies.highstreet.netconf2snmpmediator.server.streamProcessing;

/**
 *
 * @author Micha
 *
 */

public interface PreInitCallBack {

    public void Finished(Boolean succes, Object o);

}

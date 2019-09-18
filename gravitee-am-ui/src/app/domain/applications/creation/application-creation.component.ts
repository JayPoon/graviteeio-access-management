/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {Component, ViewChild} from '@angular/core';
import {MatStepper} from "@angular/material";
import {ApplicationService} from "../../../services/application.service";
import {SnackbarService} from "../../../services/snackbar.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-creation',
  templateUrl: './application-creation.component.html',
  styleUrls: ['./application-creation.component.scss']
})
export class ApplicationCreationComponent {
  public application: any = {};
  private domainId: string;
  @ViewChild ('stepper') stepper: MatStepper;

  constructor(private applicationService: ApplicationService,
              private snackbarService: SnackbarService,
              private router: Router,
              private route: ActivatedRoute) { }

  ngOnInit() {
    this.domainId = this.route.snapshot.parent.params['domainId'];
  }

  create() {
    this.applicationService.create(this.domainId, this.application).subscribe(data => {
      this.snackbarService.open("Application " + data.name + " created");
      this.router.navigate(['/domains', this.domainId, 'applications', data.id]);
    });
  }
}
